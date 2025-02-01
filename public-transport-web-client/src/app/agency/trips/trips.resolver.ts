import {
  ActivatedRouteSnapshot, ParamMap,
  ResolveFn,
  RouterStateSnapshot
} from '@angular/router';
import {TripsService} from "./trips.service";
import {inject} from "@angular/core";
import {Trips} from "../../generated/public-transport";

export class RouterQueryParams {

  constructor(private line: string, private origin: string, private destination: string, private via: string) {
  }
}

export const tripsResolver: ResolveFn<Trips> = (route: ActivatedRouteSnapshot, state: RouterStateSnapshot) => {
  const tripsService = inject(TripsService);
  return tripsService.getTrips('CHMIELNIK - TUCZĘPY', '201');
};
