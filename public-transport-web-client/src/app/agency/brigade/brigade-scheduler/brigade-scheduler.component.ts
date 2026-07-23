import {AfterViewInit, Component, signal, ViewChild} from '@angular/core';
import {DayPilot, DayPilotModule, DayPilotSchedulerComponent} from "@daypilot/daypilot-lite-angular";
import {BrigadeSchedulerService} from "./brigade-scheduler.service";
import {CommonModule, JsonPipe} from "@angular/common";

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
export class BrigadeSchedulerComponent implements AfterViewInit {

    @ViewChild("scheduler")
    scheduler!: DayPilotSchedulerComponent;

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
        scale: "CellDuration",
        cellDuration: 15,
        days: 1,
        startDate: DayPilot.Date.today(),
        eventHeight: 40,
        timeRangeSelectedHandling: "Enabled",
        onTimeRangeSelected: async (args) => {
            const scheduler = args.control;
            const modal = await DayPilot.Modal.prompt("Create a new event:", "Event 1");
            scheduler.clearSelection();
            if (modal.canceled) { return; }
            scheduler.events.add({
                start: args.start,
                end: args.end,
                id: DayPilot.guid(),
                resource: args.resource,
                text: modal.result
            });
        },
        eventMoveHandling: "Update",
        onEventMoved: (args) => {
            console.log("Event moved: " + args.e.text());
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
                { text: "Delete", onClick: (args) => { const dp = args.source.calendar; dp.events.remove(args.source); } }
            ]
        }),
    };

    constructor(private ds: BrigadeSchedulerService) {
    }

    ngAfterViewInit(): void {
        this.ds.getResources().subscribe(result => this.config.resources = result);

        var from = this.scheduler.control.visibleStart();
        var to = this.scheduler.control.visibleEnd();

        // 1. Update resources directly on the control
        this.ds.getResources().subscribe(result => {
            // This forces DayPilot to accept the new data and redraw the rows
            this.scheduler.control.update({ resources: result });
        });

        // 2. Update events directly on the control
        this.ds.getEvents(from, to).subscribe(result => {
            // This forces DayPilot to accept the new data and redraw the events
            this.scheduler.control.update({ events: result });
        });
    }

}
