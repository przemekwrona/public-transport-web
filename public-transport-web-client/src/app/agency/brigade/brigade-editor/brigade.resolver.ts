import {ResolveFn} from '@angular/router';
import {Observable} from "rxjs";
import {BrigadeBody, BrigadePayload, BrigadeService} from "../../../generated/public-transport-api";
import {inject} from "@angular/core";
import {AgencyStorageService} from "../../../auth/agency-storage.service";

export const brigadeResolver: ResolveFn<Observable<BrigadeBody>> = (route, state) => {
    const brigadeService: BrigadeService = inject(BrigadeService);
    const agencyStorageService: AgencyStorageService = inject(AgencyStorageService);

    const instance: string = agencyStorageService.getInstance();
    const brigadeName: string = route.queryParams['name'];

    const brigadePayload: BrigadePayload = {};
    brigadePayload.brigadeName = brigadeName;

    return brigadeService.getBrigadeByBrigadeName(instance, brigadePayload);
};
