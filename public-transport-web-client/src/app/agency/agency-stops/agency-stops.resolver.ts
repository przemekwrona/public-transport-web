import {ResolveFn} from '@angular/router';
import {inject} from '@angular/core';
import {Observable} from 'rxjs';
import {AgencyStops, AgencyStopsService} from '../../generated/public-transport-api';
import {LoginService} from '../../auth/login.service';

export const agencyStopsResolver: ResolveFn<AgencyStops> = (): Observable<AgencyStops> => {
    const agencyStopsService = inject(AgencyStopsService);
    const loginService = inject(LoginService);
    return agencyStopsService.getAgencyStops(loginService.getInstance());
};
