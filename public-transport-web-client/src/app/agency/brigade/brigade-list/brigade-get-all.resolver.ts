import {ResolveFn} from '@angular/router';
import {inject} from "@angular/core";
import {Observable} from "rxjs";
import {BrigadeService, GetBrigadeResponse} from "../../../generated/public-transport-api";
import {AgencyStorageService} from "../../../auth/agency-storage.service";

export const brigadeGetAllResolver: ResolveFn<Observable<GetBrigadeResponse>> = (route, state) => {
    const brigadeService = inject(BrigadeService);
    const agencyStorageService = inject(AgencyStorageService)

    return brigadeService.getBrigades(agencyStorageService.getInstance());
};
