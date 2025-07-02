import { ResolveFn } from '@angular/router';
import {AgencyDetails, AgencyService} from "../../../generated/public-transport";
import {inject} from "@angular/core";
import {AuthService} from "../../../auth/auth.service";

export const profileDetailsResolver: ResolveFn<AgencyDetails> = (route, state) => {
  const authService: AuthService = inject(AuthService);
  const agencyService: AgencyService = inject(AgencyService);

  return agencyService.getAgency(authService.getInstance());
};
