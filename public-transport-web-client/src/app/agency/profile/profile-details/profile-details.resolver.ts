import { ResolveFn } from '@angular/router';
import {AgencyDetails, AgencyService} from "../../../generated/public-transport-api";
import {inject} from "@angular/core";
import {LoginService} from "../../../auth/login.service";

export const profileDetailsResolver: ResolveFn<AgencyDetails> = (route, state) => {
  const authService: LoginService = inject(LoginService);
  const agencyService: AgencyService = inject(AgencyService);

  return agencyService.getAgency(authService.getInstance());
};
