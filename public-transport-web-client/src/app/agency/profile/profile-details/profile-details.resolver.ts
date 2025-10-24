import { ResolveFn } from '@angular/router';
import {AgencyDetails, AgencyService} from "../../../generated/public-transport-api";
import {inject} from "@angular/core";
import {LoginService} from "../../../auth/login.service";
import {AgencyStorageService} from "../../../auth/agency-storage.service";

export const profileDetailsResolver: ResolveFn<AgencyDetails> = (route, state) => {
  const agencyStorageService: AgencyStorageService = inject(AgencyStorageService);
  return agencyStorageService.getAgency();
};
