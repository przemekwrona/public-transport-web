import { ResolveFn } from '@angular/router';
import {inject} from "@angular/core";
import {BrigadeService} from "../brigade.service";
import {Observable} from "rxjs";
import {GetBrigadeResponse} from "../../../generated/public-transport";

export const brigadeGetAllTripsResolver: ResolveFn<Observable<GetBrigadeResponse>> = (route, state) => {
  const brigadeService = inject(BrigadeService);
  return brigadeService.getAllBrigades();
};
