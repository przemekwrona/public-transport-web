import { ResolveFn } from '@angular/router';
import {AgencyDetails, AgencyService} from "../../../generated/public-transport";
import {inject} from "@angular/core";

export const profileDetailsResolver: ResolveFn<AgencyDetails> = (route, state) => {
  const agencyService: AgencyService = inject(AgencyService);
  return agencyService.getAgency();
};
