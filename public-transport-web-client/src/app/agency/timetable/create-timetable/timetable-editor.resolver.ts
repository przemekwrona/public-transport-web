import {inject} from "@angular/core";
import {ActivatedRouteSnapshot, ResolveFn, RouterStateSnapshot} from "@angular/router";
import {Observable, of} from "rxjs";
import {
    GetTimetableGeneratorDetailsResponse,
    RouteId, TimetableGeneratorId, TimetableGeneratorService
} from "../../../generated/public-transport-api";
import {AgencyStorageService} from "../../../auth/agency-storage.service";
import {TimetableEditorComponentMode} from "./timetable-editor.component";

export const timetableEditorResolver: ResolveFn<Observable<GetTimetableGeneratorDetailsResponse>> = (route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<GetTimetableGeneratorDetailsResponse> => {
    const line: string = route.queryParams['line'];
    const name: string = route.queryParams['name'];
    const version: number = route.queryParams['version'];
    const timetableVersion: number = route.queryParams['timetableVersion'];
    const createdAt: string = route.queryParams['createdAt'];

    const agencyStorageService: AgencyStorageService = inject(AgencyStorageService);

    const timetableEditorComponentMode: TimetableEditorComponentMode = route.data['mode'];

    if (timetableEditorComponentMode === TimetableEditorComponentMode.CREATE) {
        return of();
    } else {
        const timetableGeneratorService: TimetableGeneratorService = inject(TimetableGeneratorService);
        const routeId: RouteId = {line: line, name: name, version: version} as RouteId;
        const timetableGeneratorId: TimetableGeneratorId = {};
        timetableGeneratorId.routeId = routeId;
        timetableGeneratorId.timetableVersion = timetableVersion;
        timetableGeneratorId.createdAt = createdAt;

        return timetableGeneratorService.findTimetableGeneratorDetails(agencyStorageService.getInstance(), timetableGeneratorId);
    }
}