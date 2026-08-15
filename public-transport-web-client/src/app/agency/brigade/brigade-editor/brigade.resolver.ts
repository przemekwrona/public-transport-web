import {ResolveFn} from '@angular/router';
import {Observable} from "rxjs";
import {
    BrigadeService, GetBrigadeDetailsResponse
} from "../../../generated/public-transport-api";
import {inject} from "@angular/core";
import {AgencyStorageService} from "../../../auth/agency-storage.service";

export const brigadeResolver: ResolveFn<Observable<GetBrigadeDetailsResponse>> = (route, state) => {
    const brigadeService: BrigadeService = inject(BrigadeService);
    const agencyStorageService: AgencyStorageService = inject(AgencyStorageService);

    const instance: string = agencyStorageService.getInstance();
    const brigadeCode: string = route.paramMap.get('brigadeCode');

    return brigadeService.getBrigadeDetails(instance, brigadeCode);
};
