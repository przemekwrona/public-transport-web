import {AfterViewInit, Component, ElementRef, Input, NgZone, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {DayPilot, DayPilotModule, DayPilotSchedulerComponent} from "@daypilot/daypilot-lite-angular";
import {CommonModule} from "@angular/common";
import moment from "moment";
import {MatDialog} from "@angular/material/dialog";
import {
    OnTimeRangeAndTripSelected,
    OnTimeRangeSelectedModalComponent
} from "./on-time-range-selected-modal/on-time-range-selected-modal.component";
import {
    BrigadeTimetableModalComponent
} from "./brigade-timetable-modal/brigade-timetable-modal.component";
import {
    BrigadeEvent, BrigadeGroupBody,
    BrigadeResource,
    BrigadeService, GetAllTripsResponse, GetBrigadeDetailsResponse,
    NextCalendarResourceSequenceResponse,
    PutBrigadeEventBody, ResourceOptimizerService, ResourceService, TripId2
} from "../../../generated/public-transport-api";
import {AgencyStorageService} from "../../../auth/agency-storage.service";
import {MatIconModule} from "@angular/material/icon";
import {RouterModule} from "@angular/router";

const SCHEDULER_TIME_HEADERS: DayPilot.TimeHeaderData[] = [
    {groupBy: "Day", format: "Dzień 1"},
    {groupBy: "Hour"},
    {groupBy: "Cell", format: "mm"},
];

const SCHEDULER_ZOOM_STEPS: { cellWidth: number; label: string }[] = [
    {cellWidth: 120, label: "400%"},
    {cellWidth: 90, label: "300%"},
    {cellWidth: 60, label: "200%"},
    {cellWidth: 45, label: "150%"},
    {cellWidth: 30, label: "100%"},
    {cellWidth: 27, label: "90%"},
    {cellWidth: 24, label: "80%"},
    {cellWidth: 21, label: "70%"},
    {cellWidth: 18, label: "60%"},
    {cellWidth: 15, label: "50%"},
];

const DEFAULT_SCHEDULER_ZOOM = SCHEDULER_ZOOM_STEPS.findIndex(step => step.cellWidth === 30);

@Component({
    selector: 'app-brigade-scheduler',
    imports: [
        CommonModule,
        DayPilotModule,
        MatIconModule,
        RouterModule,
    ],
    providers: [],
    templateUrl: './brigade-scheduler.component.html',
    styleUrl: './brigade-scheduler.component.scss'
})
export class BrigadeSchedulerComponent implements OnInit, AfterViewInit, OnDestroy {

    @ViewChild("scheduler")
    scheduler!: DayPilotSchedulerComponent;

    @ViewChild("schedulerViewport")
    schedulerViewport?: ElementRef<HTMLElement>;

    @Input() brigadeCode: string = '';
    @Input() brigadeBody: BrigadeGroupBody = {} as BrigadeGroupBody;
    @Input() defaultRoutes: GetAllTripsResponse = {} as GetAllTripsResponse;

    events: DayPilot.EventData[] = [];

    readonly alignOptions: { minutes: number; label: string }[] = [
        { minutes: 5, label: '5m' },
        { minutes: 10, label: '10m' },
        { minutes: 15, label: '15m' },
        { minutes: 20, label: '20m' },
        { minutes: 30, label: '30m' },
        { minutes: 60, label: '1h' },
    ];

    selectedAlignMinutes: number | null = null;
    isOptimizing = false;

    readonly zoomSteps = SCHEDULER_ZOOM_STEPS;
    zoomIndex = DEFAULT_SCHEDULER_ZOOM;

    private zoomWheelLocked = false;
    private pointerClientX: number | null = null;
    private removeZoomWheelListener?: () => void;

    config: DayPilot.SchedulerConfig = {
        locale: "pl-pl",
        // cellWidthSpec: "Fixed",
        cellWidth: SCHEDULER_ZOOM_STEPS[DEFAULT_SCHEDULER_ZOOM].cellWidth,
        timeHeaders: SCHEDULER_TIME_HEADERS,
        zoomPosition: "middle",
        zoom: DEFAULT_SCHEDULER_ZOOM,
        zoomLevels: SCHEDULER_ZOOM_STEPS.map(step => ({
            name: step.label,
            properties: {
                scale: "CellDuration",
                cellDuration: 15,
                cellWidth: step.cellWidth,
                timeHeaders: SCHEDULER_TIME_HEADERS,
            },
        })),
        useEventBoxes: "Never",
        scale: "CellDuration",
        // scale: "Minute",
        cellDuration: 15,
        days: 1,
        startDate: DayPilot.Date.today(),
        eventHeight: 40,
        timeRangeSelectedHandling: "Enabled",
        snapToGrid: false,
        rowMarginBottom: 0,
        onTimeRangeSelected: async (args) => {
            // Clear the temporary selection highlight in DayPilot
            args.control.clearSelection();

            // Open your own modal, passing in the start/end times
            this.openMyCreateModal(args.start, args.end, args.resource, args.control);

            const scheduler = args.control;
            // const modal = await DayPilot.Modal.prompt("Create a new event:", "Event 1");
            scheduler.clearSelection();
            // if (modal.canceled) {
            //     return;
            // }
            // scheduler.events.add({
            //     start: args.start,
            //     end: args.end,
            //     id: DayPilot.guid(),
            //     resource: args.resource,
            //     text: modal.result
            // });
        },
        eventMoveHandling: "Update",
        onEventMoved: (args) => {
            this.updateBrigadeEvent(args);
        },
        onEventClick: (args) => {
            console.log('On event click');
        },
        eventResizeHandling: "Update",
        onEventResized: (args) => {
            console.log("Event resized: " + args.e.text());
        },
        eventDeleteHandling: "Update",
        onEventDeleted: (args) => {
            this.deleteBrigadeEvent(args);
        },
        eventRightClickHandling: "ContextMenu",
        contextMenu: new DayPilot.Menu({
            items: [
                {
                    text: "Delete", onClick: (args) => {
                        const dp = args.source.calendar;
                        dp.events.remove(args.source);
                    }
                }
            ]
        }),
    };

    constructor(private agencyStorage: AgencyStorageService, private brigadeService: BrigadeService, private resourceService: ResourceService, private resourceOptimizerService: ResourceOptimizerService, private dialog: MatDialog, private ngZone: NgZone) {
    }

    ngOnInit(): void {
    }

    ngAfterViewInit(): void {
        this.applyBrigadeToScheduler(this.brigadeBody);
        this.bindZoomWheel();
    }

    ngOnDestroy(): void {
        this.removeZoomWheelListener?.();
    }

    get zoomLabel(): string {
        return this.zoomSteps[this.zoomIndex].label;
    }

    get canZoomIn(): boolean {
        return this.zoomIndex > 0;
    }

    get canZoomOut(): boolean {
        return this.zoomIndex < this.zoomSteps.length - 1;
    }

    zoomIn(): void {
        this.setZoom(this.zoomIndex - 1);
    }

    zoomOut(): void {
        this.setZoom(this.zoomIndex + 1);
    }

    private setZoom(index: number, clientX: number | null = this.pointerClientX): void {
        const control = this.scheduler?.control;
        if (!control || index < 0 || index >= this.zoomSteps.length || index === this.zoomIndex) {
            return;
        }

        const anchor = this.captureTimelineAnchor(clientX);
        this.zoomIndex = index;
        control.zoom.setActive(index, "left");
        this.restoreTimelineAnchor(anchor);
    }

    private captureTimelineAnchor(clientX: number | null): { date: DayPilot.Date; offsetX: number } | null {
        const control = this.scheduler?.control;
        const scrollElement = this.timelineScrollElement();
        if (!control || !scrollElement) {
            return null;
        }

        const rect = scrollElement.getBoundingClientRect();
        if (rect.width <= 0) {
            return null;
        }

        const pointerX = clientX ?? rect.left + rect.width / 2;
        const offsetX = Math.min(Math.max(pointerX - rect.left, 0), rect.width);
        const date = control.getDate(control.getScrollX() + offsetX, true);
        if (!date) {
            return null;
        }

        return {date, offsetX};
    }

    private restoreTimelineAnchor(anchor: { date: DayPilot.Date; offsetX: number } | null): void {
        const control = this.scheduler?.control;
        const scrollElement = this.timelineScrollElement();
        if (!control || !scrollElement || !anchor) {
            return;
        }

        const timelineEnd = new DayPilot.Date(control.startDate).addDays(control.days ?? 1);
        const contentWidth = this.timelinePixel(control, timelineEnd);
        const nextScroll = this.timelinePixel(control, anchor.date) - anchor.offsetX;
        const maxScroll = Math.max(0, contentWidth - scrollElement.clientWidth);
        const scrollX = Math.min(Math.max(0, nextScroll), maxScroll);
        const timeScroll = (control as DayPilot.Scheduler & { divTimeScroll?: HTMLElement }).divTimeScroll;
        if (timeScroll) {
            timeScroll.scrollLeft = scrollX;
        }
        scrollElement.scrollLeft = scrollX;
        control.setScrollX(scrollX);
    }

    private timelinePixel(control: DayPilot.Scheduler, date: DayPilot.Date): number {
        const start = new DayPilot.Date(control.startDate);
        const cellDurationMs = (control.cellDuration ?? 15) * 60 * 1000;
        const cellWidth = control.cellWidth ?? 30;
        return Math.floor(cellWidth * (date.getTime() - start.getTime()) / cellDurationMs);
    }

    private timelineScrollElement(): HTMLElement | null {
        const control = this.scheduler?.control as (DayPilot.Scheduler & { nav?: { scroll?: HTMLElement } }) | undefined;
        return control?.nav?.scroll ?? null;
    }

    private bindZoomWheel(): void {
        const element = this.schedulerViewport?.nativeElement;
        if (!element) {
            return;
        }

        const onMouseMove = (event: MouseEvent) => {
            const scrollElement = this.timelineScrollElement();
            if (!scrollElement) {
                return;
            }

            const rect = scrollElement.getBoundingClientRect();
            if (event.clientX >= rect.left && event.clientX <= rect.right) {
                this.pointerClientX = event.clientX;
            }
        };
        const onWheel = (event: WheelEvent) => {
            if (!event.ctrlKey && !event.metaKey) {
                return;
            }

            event.preventDefault();
            this.pointerClientX = event.clientX;
            if (this.zoomWheelLocked) {
                return;
            }

            this.zoomWheelLocked = true;
            const nextIndex = event.deltaY < 0 ? this.zoomIndex - 1 : this.zoomIndex + 1;
            this.ngZone.run(() => this.setZoom(nextIndex, event.clientX));
            window.setTimeout(() => {
                this.zoomWheelLocked = false;
            }, 140);
        };

        element.addEventListener("mousemove", onMouseMove);
        element.addEventListener("wheel", onWheel, {passive: false});
        this.removeZoomWheelListener = () => {
            element.removeEventListener("mousemove", onMouseMove);
            element.removeEventListener("wheel", onWheel);
        };
    }

    // --- CREATE NEW EVENT ---
    openTimetableModal() {
        this.dialog.open(BrigadeTimetableModalComponent, {
            data: {
                brigadeCode: this.brigadeCode,
                calendarCode: this.brigadeBody.calendarSymbolId?.calendarItemId?.code,
                calendarSymbol: this.brigadeBody.calendarSymbolId?.symbol,
            }
        });
    }

    public openCreateEventPanel(): void {
        const resources = this.scheduler.control.resources ?? [];
        const lastResource = resources[resources.length - 1];

        if (lastResource) {
            this.openCreateModalForResource(String(lastResource.id));
            return;
        }

        this.createResource((resourceId) => this.openCreateModalForResource(resourceId));
    }

    private openCreateModalForResource(resourceId: string): void {
        const scheduler = this.scheduler.control;
        const resourceEvents = (scheduler.events.list ?? [])
            .filter(event => String(event.resource) === resourceId);

        let start = new DayPilot.Date(this.config.startDate).addHours(6);
        if (resourceEvents.length > 0) {
            start = resourceEvents.reduce((latest, event) => {
                const eventEnd = new DayPilot.Date(event.end);
                return eventEnd.getTime() > latest.getTime() ? eventEnd : latest;
            }, new DayPilot.Date(resourceEvents[0].end));
        }

        this.openMyCreateModal(start, start.addMinutes(15), resourceId, scheduler);
    }

    openMyCreateModal(start: DayPilot.Date, end: DayPilot.Date, resource: DayPilot.ResourceId, dpControl: DayPilot.Scheduler) {
        const dialogRef = this.dialog.open(OnTimeRangeSelectedModalComponent, {
            width: '920px',
            data: {
                start: start.toString(),
                end: end.toString(),
                resourceId: resource.toString(),
                defaultRoutes: this.defaultRoutes
            }
        });

        dialogRef.afterClosed().subscribe((result: OnTimeRangeAndTripSelected) => {
            // If the user cancelled the modal, result is null/undefined
            if (!result) return;

            // Add the new event to the DayPilot calendar
            const startDate = moment(result.start).format('HH:mm');
            const endDate = moment(result.end).format('HH:mm');

            const instance: string = this.agencyStorage.getInstance();
            const tripId = result.tripId;

            this.brigadeService.getNextBrigadeEventSequence(instance, this.brigadeCode, this.brigadeBody.calendarSymbolId.calendarItemId.code, this.brigadeBody.calendarSymbolId.symbol, result.resourceId).subscribe((response) => {
                const line = result.tripId.routeId.line;
                const name = result.tripId.routeId.name;

                const event: DayPilot.EventData = {
                    start: new DayPilot.Date(result.start),
                    end: new DayPilot.Date(result.end),
                    id: response.sequenceHex,
                    resource: result.resourceId,
                    text: `${startDate}-${endDate}\n${line} ${result.origin} - ${result.destination} ${result.tripId.variantMode}`,
                    tags: {
                        line,
                        name,
                        tripId: tripId,
                        sequence: response.sequence,
                        sequenceHex: response.sequenceHex,
                    }
                } as DayPilot.EventData;

                const startMoment = moment(result.start);
                const endMoment = moment(result.end);
                const midnight = startMoment.clone().startOf('day');

                const putBrigadeEventBody: PutBrigadeEventBody = {
                    startSecond: startMoment.diff(midnight, 'seconds'),
                    endSecond: endMoment.diff(midnight, 'seconds'),
                    line,
                    name,
                    sequence: response.sequence,
                    sequenceHex: response.sequenceHex,
                    tripId
                };

                this.brigadeService.putBrigadeEvent(
                    instance,
                    this.brigadeCode,
                    this.brigadeBody.calendarSymbolId.calendarItemId.code,
                    this.brigadeBody.calendarSymbolId.symbol,
                    result.resourceId,
                    putBrigadeEventBody
                ).subscribe(() => {
                    dpControl.events.add(event);
                });
            });

        });
    }

    // --- UPDATE EVENT (MOVE) ---
    updateBrigadeEvent(args: DayPilot.SchedulerEventMovedArgs): void {
        const tags = args.e.data.tags ?? {};
        const line: string = tags.line;
        const name: string = tags.name;
        const tripId: TripId2 = tags.tripId;
        const sequence: number = tags.sequence;
        const sequenceHex: string = tags.sequenceHex ?? String(args.e.id());

        const startMoment = moment(args.newStart.toString());
        const endMoment = moment(args.newEnd.toString());
        const midnight = startMoment.clone().startOf('day');
        const putBrigadeEventBody: PutBrigadeEventBody = {
            startSecond: startMoment.diff(midnight, 'seconds'),
            endSecond: endMoment.diff(midnight, 'seconds'),
            line,
            name,
            sequence,
            sequenceHex,
            tripId
        };

        const instance: string = this.agencyStorage.getInstance();

        this.brigadeService.putBrigadeEvent(
            instance,
            this.brigadeCode,
            this.brigadeBody.calendarSymbolId.calendarItemId.code,
            this.brigadeBody.calendarSymbolId.symbol,
            String(args.newResource),
            putBrigadeEventBody
        ).subscribe(() => {
            const startDate = startMoment.format('HH:mm');
            const endDate = endMoment.format('HH:mm');
            args.e.text(`${startDate}-${endDate}\n${line} ${name}`);
        });
    }

    // --- DELETE EVENT ---
    deleteBrigadeEvent(args: DayPilot.SchedulerEventDeletedArgs): void {
        const tags = args.e.data.tags ?? {};
        const sequenceHex: string = tags.sequenceHex ?? String(args.e.id());
        const resourceCode: string = String(args.e.resource());
        const instance: string = this.agencyStorage.getInstance();

        this.brigadeService.deleteBrigadeEvent(
            instance,
            this.brigadeCode,
            this.brigadeBody.calendarSymbolId.calendarItemId.code,
            this.brigadeBody.calendarSymbolId.symbol,
            resourceCode,
            sequenceHex
        ).subscribe();
    }

    public addResource(): void {
        this.createResource();
    }

    public removeLastResource(): void {
        const scheduler = this.scheduler.control;
        const resources = [...(scheduler.resources ?? [])];
        if (resources.length < 2) {
            return;
        }

        const lastResourceId = String(resources[resources.length - 1].id);
        const previousResourceId = String(resources[resources.length - 2].id);
        const events = [...(scheduler.events.list ?? [])];
        const instance = this.agencyStorage.getInstance();

        for (const event of events) {
            if (String(event.resource) !== lastResourceId) {
                continue;
            }

            event.resource = previousResourceId;

            const tags = event.tags ?? {};
            const startMoment = moment(event.start.toString());
            const endMoment = moment(event.end.toString());
            const midnight = startMoment.clone().startOf('day');

            const putBrigadeEventBody: PutBrigadeEventBody = {
                startSecond: startMoment.diff(midnight, 'seconds'),
                endSecond: endMoment.diff(midnight, 'seconds'),
                line: tags.line,
                name: tags.name,
                sequence: tags.sequence,
                sequenceHex: tags.sequenceHex ?? String(event.id),
                tripId: tags.tripId
            };

            this.brigadeService.putBrigadeEvent(
                instance,
                this.brigadeCode,
                this.brigadeBody.calendarSymbolId.calendarItemId.code,
                this.brigadeBody.calendarSymbolId.symbol,
                previousResourceId,
                putBrigadeEventBody
            ).subscribe();
        }

        scheduler.update({
            resources: resources.slice(0, -1),
            events
        });
    }

    private createResource(afterCreate?: (resourceId: string) => void): void {
        const instance: string = this.agencyStorage.getInstance();

        this.brigadeService.getNextCalendarResourceSequence(instance, this.brigadeCode, this.brigadeBody.calendarSymbolId.calendarItemId.code, this.brigadeBody.calendarSymbolId.symbol).subscribe((response: NextCalendarResourceSequenceResponse) => {
            const resource = {
                id: response.sequenceHex,
                name: `#${response.sequenceHex}`,
                expanded: true
            };

            this.scheduler.control.update({
                resources: [...(this.scheduler.control.resources ?? []), resource]
            });
            afterCreate?.(response.sequenceHex);
        });
    }

    public optimizeBrigades(): void {
        if (this.isOptimizing) {
            return;
        }

        const calendarCode = this.brigadeBody.calendarSymbolId?.calendarItemId?.code;
        const symbol = this.brigadeBody.calendarSymbolId?.symbol;
        if (!this.brigadeCode || !calendarCode || !symbol) {
            return;
        }

        this.isOptimizing = true;
        this.resourceOptimizerService.optimizeBrigades(
            this.agencyStorage.getInstance(),
            this.brigadeCode,
            calendarCode,
            symbol
        ).subscribe({
            next: () => {
                this.brigadeService.getBrigadeDetails(this.agencyStorage.getInstance(), this.brigadeCode).subscribe({
                    next: (brigadeDetails: GetBrigadeDetailsResponse) => {
                        const brigadeBody = brigadeDetails.brigade.brigades.find((brigade) => brigade.calendarSymbolId.symbol === symbol);
                        if (brigadeBody) {
                            this.brigadeBody = brigadeBody;
                            this.applyBrigadeToScheduler(this.brigadeBody);
                        }
                        this.isOptimizing = false;
                    },
                    error: () => {
                        this.isOptimizing = false;
                    }
                });
            },
            error: () => {
                this.isOptimizing = false;
            }
        });
    }

    public clearGroup(): void {
        this.resourceService.deleteResource(this.agencyStorage.getInstance(), this.brigadeCode, this.brigadeBody.calendarSymbolId.calendarItemId.code, this.brigadeBody.calendarSymbolId.symbol).subscribe(() => {
            this.brigadeService.getBrigadeDetails(this.agencyStorage.getInstance(), this.brigadeCode).subscribe((brigadeDetails: GetBrigadeDetailsResponse) => {
                const brigadeBody = brigadeDetails.brigade.brigades.find((brigade) => brigade.calendarSymbolId.symbol === this.brigadeBody.calendarSymbolId.symbol);
                if (!brigadeBody) {
                    return;
                }

                this.brigadeBody = brigadeBody;
                this.applyBrigadeToScheduler(this.brigadeBody);
            });
        });
    }

    private applyBrigadeToScheduler(brigade: BrigadeGroupBody): void {
        const resources = (brigade.brigadeResources ?? []).map((resource: BrigadeResource) => {
            return {name: `#${resource.sequenceHex}`, id: resource.sequenceHex, expanded: true};
        }) as any[];

        const events: DayPilot.EventData[] = (brigade.brigadeResources ?? []).flatMap((resource: BrigadeResource) => {
            return (resource.events ?? []).map((event: BrigadeEvent) => {
                const departureTime = moment().startOf('day').add(event.startSecond, 'seconds');
                const arrivalTime = moment().startOf('day').add(event.endSecond, 'seconds');

                return {
                    id: event.sequenceHex,
                    resource: resource.sequenceHex,
                    start: departureTime.format('YYYY-MM-DDTHH:mm:ss'),
                    end: arrivalTime.format('YYYY-MM-DDTHH:mm:ss'),
                    text: `${departureTime.format('HH:mm')}-${arrivalTime.format('HH:mm')} \n${event.tripId.variantMode} ${event.line} ${event.name}`,
                    color: '#e69138',
                    tags: {
                        line: event.line,
                        name: event.name,
                        sequence: event.sequence,
                        sequenceHex: event.sequenceHex,
                        tripId: event.tripId,
                    }
                } as DayPilot.EventData;
            });
        });

        this.scheduler.control.update({resources, events});

        const startSeconds = (brigade.brigadeResources ?? [])
            .flatMap(resource => resource.events ?? [])
            .map(event => event.startSecond);

        if (startSeconds.length === 0) {
            return;
        }

        const minStartSeconds = startSeconds.reduce((current, next) => current <= next ? current : next);
        const firstDate = moment().startOf('day').add(minStartSeconds, 'seconds').subtract(45, 'minutes');
        this.scheduler.control.scrollTo(firstDate.format('yyyy-MM-DDTHH:mm:SS'));
    }

    public alignToMinutes(minutes: number): void {
        const allowedMinutes = this.alignOptions.map(option => option.minutes);
        if (!allowedMinutes.includes(minutes)) {
            return;
        }

        this.selectedAlignMinutes = minutes;

        const scheduler = this.scheduler.control;
        const resources = scheduler.resources ?? [];
        const events = [...(scheduler.events.list ?? [])];
        const intervalSeconds = minutes * 60;
        const instance = this.agencyStorage.getInstance();

        for (const resource of resources) {
            const resourceId = String(resource.id);
            const resourceEvents = events.filter(event => String(event.resource) === resourceId);

            for (const event of resourceEvents) {
                const start = moment(event.start.toString());
                const end = moment(event.end.toString());
                const midnight = start.clone().startOf('day');
                const durationSeconds = end.diff(start, 'seconds');
                const startSeconds = start.diff(midnight, 'seconds');
                const alignedStartSeconds = Math.round(startSeconds / intervalSeconds) * intervalSeconds;

                if (alignedStartSeconds === startSeconds) {
                    continue;
                }

                const alignedEndSeconds = alignedStartSeconds + durationSeconds;
                const alignedStart = midnight.clone().add(alignedStartSeconds, 'seconds');
                const alignedEnd = midnight.clone().add(alignedEndSeconds, 'seconds');
                const tags = event.tags ?? {};

                event.start = alignedStart.format('YYYY-MM-DDTHH:mm:ss');
                event.end = alignedEnd.format('YYYY-MM-DDTHH:mm:ss');
                event.text = `${alignedStart.format('HH:mm')}-${alignedEnd.format('HH:mm')}\n${tags.line} ${tags.name}`;

                const putBrigadeEventBody: PutBrigadeEventBody = {
                    startSecond: alignedStartSeconds,
                    endSecond: alignedEndSeconds,
                    line: tags.line,
                    name: tags.name,
                    sequence: tags.sequence,
                    sequenceHex: tags.sequenceHex ?? String(event.id),
                    tripId: tags.tripId
                };

                this.brigadeService.putBrigadeEvent(
                    instance,
                    this.brigadeCode,
                    this.brigadeBody.calendarSymbolId.calendarItemId.code,
                    this.brigadeBody.calendarSymbolId.symbol,
                    resourceId,
                    putBrigadeEventBody
                ).subscribe();
            }
        }

        scheduler.update({events});
    }

}
