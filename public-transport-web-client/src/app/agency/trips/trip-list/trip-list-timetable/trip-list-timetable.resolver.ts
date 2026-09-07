import {inject} from "@angular/core";
import {ActivatedRouteSnapshot, ResolveFn, RouterStateSnapshot} from "@angular/router";
import {Observable} from "rxjs";
import {RouteService, RouteStops} from "../../../../generated/public-transport-api";
import {LoginService} from "../../../../auth/login.service";
import {getRouteParam} from "../trip-list.resolver";

export const tripListTimetableResolver: ResolveFn<Observable<RouteStops>> = (
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
): Observable<RouteStops> => {
    const authService: LoginService = inject(LoginService);
    const routeService: RouteService = inject(RouteService);
    const routeCode: string = getRouteParam(route, 'routeCode')!;

    return routeService.getRouteStops(authService.getInstance(), routeCode);
};
