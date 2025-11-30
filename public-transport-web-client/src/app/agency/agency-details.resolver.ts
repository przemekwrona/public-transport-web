import {ActivatedRouteSnapshot, ResolveFn, RouterStateSnapshot} from "@angular/router";
import {Observable} from "rxjs";
import {inject} from "@angular/core";
import {AgencyStorageService} from "../auth/agency-storage.service";
import {AgencyDetails} from "../generated/public-transport-api";

export const agencyDetailsResolver: ResolveFn<Observable<AgencyDetails>> = (route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<AgencyDetails> => {
    const agencyDetailService: AgencyStorageService = inject(AgencyStorageService);

    return agencyDetailService.getAgency();
}