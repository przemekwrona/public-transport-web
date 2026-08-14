import {AfterViewInit, Component, Input, OnInit, signal, ViewChild} from '@angular/core';
import {DayPilot, DayPilotModule, DayPilotSchedulerComponent} from "@daypilot/daypilot-lite-angular";
import {BrigadeSchedulerService} from "./brigade-scheduler.service";
import {CommonModule} from "@angular/common";
import {BrigadeModel} from "../brigade-editor/brigade-editor.model";
import moment from "moment";
import ResourceData = DayPilot.ResourceData;
import {MatDialog} from "@angular/material/dialog";
import {
    OnTimeRangeAndTripSelected,
    OnTimeRangeSelectedModalComponent
} from "./on-time-range-selected-modal/on-time-range-selected-modal.component";
import {BrigadeBodyV2, BrigadeResource} from "../../../generated/public-transport-api";

@Component({
    selector: 'app-brigade-scheduler',
    imports: [
        CommonModule,
        DayPilotModule,
    ],
    providers: [
        BrigadeSchedulerService
    ],
    templateUrl: './brigade-scheduler.component.html',
    styleUrl: './brigade-scheduler.component.scss'
})
export class BrigadeSchedulerComponent implements OnInit, AfterViewInit {

    @ViewChild("scheduler")
    scheduler!: DayPilotSchedulerComponent;

    @Input() brigadeEvent: BrigadeModel[] = []
    @Input() brigadeResources: BrigadeResource[];

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
            this.openMyCreateModal(args.start, args.end, args.control);

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
            console.log("Event moved: " + args.e.text());
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
            console.log("Event deleted: " + args.e.text());
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

    constructor(private ds: BrigadeSchedulerService, private dialog: MatDialog) {
    }

    ngOnInit(): void {
    }

    ngAfterViewInit(): void {
        var from = this.scheduler.control.visibleStart();
        var to = this.scheduler.control.visibleEnd();

        // 1. Update resources directly on the control
        const resources = this.brigadeResources
            .map((resource: BrigadeResource) => {
                return  {name: `#${resource.sequenceHex}`, id: resource.sequenceHex, expanded: true};
            }) as any[];
        this.scheduler.control.update({resources: resources});

        const events = this.brigadeEvent.map(e => {
            const event: DayPilot.EventData = {} as DayPilot.EventData;
            event.resource = "0001";

            const departureTime = moment(e.departureTime, "HH:mm");
            const arrivalTime = departureTime.clone().add(e.travelTimeInSeconds + 60, 'second');
            const r = Math.random();
            return {
                id: `${e.line}_${e.mode}_${r}`,
                resource: "GA",
                start: departureTime.format('yyyy-MM-DDTHH:mm:SS'),
                end: arrivalTime.format('yyyy-MM-DDTHH:mm:SS'),
                text: `${departureTime.format('HH:mm')}-${arrivalTime.format('HH:mm')} \n${e.line} ${e.name} ${e.mode}`,
                color: "#e69138"
            } as DayPilot.EventData
        });
        // 2. Update events directly on the control
        this.scheduler.control.update({events: events});

        const firstDate = this.brigadeEvent
            .map(brigade => brigade.departureTime)
            .map(departureTime => moment(departureTime, "HH:mm"))
            .reduce((current, next) => current.isBefore(next) ? current : next)
            .subtract(45, 'minutes');

        this.scheduler.control.scrollTo(firstDate.format('yyyy-MM-DDTHH:mm:SS'));
    }

    // --- CREATE NEW EVENT ---
    openMyCreateModal(start: DayPilot.Date, end: DayPilot.Date, dpControl: DayPilot.Scheduler) {
        const dialogRef = this.dialog.open(OnTimeRangeSelectedModalComponent, {
            data: {start: start.toString(), end: end.toString()}
        });

        dialogRef.afterClosed().subscribe((result: OnTimeRangeAndTripSelected) => {
            // If the user cancelled the modal, result is null/undefined
            if (!result) return;

            // Add the new event to the DayPilot calendar
            const startDate = moment(result.start).format('HH:mm');
            const endDate = moment(result.end).format('HH:mm');
            const event: DayPilot.EventData = {
                start: new DayPilot.Date(result.start),
                end: new DayPilot.Date(result.end),
                id: DayPilot.guid(),
                resource: 'GA',
                text: `${startDate}-${endDate}\n${result.tripId.routeId.line} ${result.origin} - ${result.destination} ${result.tripId.variantMode}` // Data returned from your modal
            } as DayPilot.EventData;

            dpControl.events.add(event);
        });
    }

    public addBrigade(): void {
        const resources: ResourceData[] = this.scheduler.control.resources;
        const emptyResource: ResourceData = {} as ResourceData;
        emptyResource.name = 'Brygada 2';
        emptyResource.id = 'GB';

        this.scheduler.control.update({resources: [...resources, ...[emptyResource]]});
    }

}
