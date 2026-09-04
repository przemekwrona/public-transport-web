import {AfterViewInit, Component, Input, OnInit, ViewChild} from '@angular/core';
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
    BrigadeService, GetAllTripsResponse,
    NextCalendarResourceSequenceResponse,
    PutBrigadeEventBody, TripId2
} from "../../../generated/public-transport-api";
import {AgencyStorageService} from "../../../auth/agency-storage.service";

@Component({
    selector: 'app-brigade-scheduler',
    imports: [
        CommonModule,
        DayPilotModule,
    ],
    providers: [],
    templateUrl: './brigade-scheduler.component.html',
    styleUrl: './brigade-scheduler.component.scss'
})
export class BrigadeSchedulerComponent implements OnInit, AfterViewInit {

    @ViewChild("scheduler")
    scheduler!: DayPilotSchedulerComponent;

    @Input() brigadeCode: string = '';
    @Input() brigadeBody: BrigadeGroupBody = {} as BrigadeGroupBody;
    @Input() defaultRoutes: GetAllTripsResponse = {} as GetAllTripsResponse;

    events: DayPilot.EventData[] = [];

    config: DayPilot.SchedulerConfig = {
        locale: "pl-pl",
        // cellWidthSpec: "Fixed",
        cellWidth: 30,
        timeHeaders: [
            {
                groupBy: "Day",
                // format: "dddd, d MMMM yyyy",
                format: "Dzień 1",
            },
            {
                groupBy: "Hour",
            },
            {
                groupBy: "Cell",
                format: "mm",
            },
        ],
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

    constructor(private agencyStorage: AgencyStorageService, private brigadeService: BrigadeService, private dialog: MatDialog) {
    }

    ngOnInit(): void {
    }

    ngAfterViewInit(): void {
        var from = this.scheduler.control.visibleStart();
        var to = this.scheduler.control.visibleEnd();

        // 1. Update resources directly on the control
        const resources = this.brigadeBody.brigadeResources
            .map((resource: BrigadeResource) => {
                return {name: `#${resource.sequenceHex}`, id: resource.sequenceHex, expanded: true};
            }) as any[];
        this.scheduler.control.update({resources: resources});

        const events: DayPilot.EventData[] = this.brigadeBody.brigadeResources.flatMap((resource: BrigadeResource) => {
            return resource.events.map((event: BrigadeEvent) => {
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

        // 2. Update events directly on the control
        this.scheduler.control.update({events: events});

        const minStartSeconds: number = this.brigadeBody.brigadeResources
            .flatMap(brigade => brigade.events)
            .map(departureTime => departureTime.startSecond)
            .reduce((current, next) => current <= next ? current : next);

        const firstDate = moment().startOf('day').add(minStartSeconds, 'seconds').subtract(45, 'minutes');

        this.scheduler.control.scrollTo(firstDate.format('yyyy-MM-DDTHH:mm:SS'));
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

    openMyCreateModal(start: DayPilot.Date, end: DayPilot.Date, resource: DayPilot.ResourceId, dpControl: DayPilot.Scheduler) {
        const dialogRef = this.dialog.open(OnTimeRangeSelectedModalComponent, {
            width: '920px',
            data: {start: start.toString(), end: end.toString(), resourceId: resource.toString(), defaultRoutes: this.defaultRoutes}
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
        console.log(tripId);
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
        });
    }

    public alignToMinutes(minutes: number): void {
        const allowedMinutes = [5, 10, 15, 20, 30, 60];
        if (!allowedMinutes.includes(minutes)) {
            return;
        }

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
