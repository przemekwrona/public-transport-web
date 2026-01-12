import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, RouterModule} from "@angular/router";
import {
    Status,
    TimetableGeneratorDeletionRequest,
    TimetableGeneratorFindAllItem,
    TimetableGeneratorFindAllResponse, TimetableGeneratorService
} from "../../../generated/public-transport-api";
import {CommonModule} from "@angular/common";
import {size} from "lodash";
import {AgencyStorageService} from "../../../auth/agency-storage.service";
import {NotificationService} from "../../../shared/notification.service";

@Component({
    selector: 'app-timetable-list',
    standalone: true,
    imports: [
        CommonModule,
        RouterModule
    ],
    templateUrl: './timetable-list.component.html',
    styleUrl: './timetable-list.component.scss'
})
export class TimetableListComponent implements OnInit {

    public response: TimetableGeneratorFindAllResponse;

    constructor(private route: ActivatedRoute, private timetableGeneratorService: TimetableGeneratorService, private agencyStorageService: AgencyStorageService, private notificationService: NotificationService) {
    }

    public hasTimetables(): boolean {
        return size(this.response.items) > 0;
    }

    ngOnInit(): void {
        this.response = this.route.snapshot.data["timetables"]
    }

    public openTimetableGenerator(timetableGenerator: TimetableGeneratorFindAllItem): void {

    }

    public deleteGeneratedTimetable(timetableGenerator: TimetableGeneratorFindAllItem): void {
        const request: TimetableGeneratorDeletionRequest = {};
        request.routeId = {
            line: timetableGenerator.timetableGeneratorId.routeId.line,
            name: timetableGenerator.timetableGeneratorId.routeId.name,
            version: timetableGenerator.timetableGeneratorId.routeId.version,
        };
        request.createdDate = timetableGenerator.createdAt;
        this.timetableGeneratorService.deleteTimetableGenerator(this.agencyStorageService.getInstance(), request).subscribe((deleteStatusResponse: Status) => {
            if (deleteStatusResponse.status === Status.StatusEnum.Deleted) {
                this.notificationService.showSuccess("Udało się usunąć Generator Rozkładu Jazdy");
                this.timetableGeneratorService.findAll(this.agencyStorageService.getInstance()).subscribe((timetableGeneratorsResponse: TimetableGeneratorFindAllResponse) => this.response = timetableGeneratorsResponse);
            }
        });
    }

}
