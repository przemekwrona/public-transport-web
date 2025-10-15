import {ResolveFn} from '@angular/router';
import {AgenciesAdminResponse, AgencyService} from "../../../generated/public-transport-api";
import {inject} from "@angular/core";

export const agenciesResolver: ResolveFn<AgenciesAdminResponse> = (route, state) => {
    const agencyService: AgencyService = inject(AgencyService);

    return agencyService.findAllAgencies();
};
