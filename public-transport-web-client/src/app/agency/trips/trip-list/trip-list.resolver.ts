import {inject} from "@angular/core";
import {ActivatedRouteSnapshot, ResolveFn, RouterStateSnapshot} from "@angular/router";
import {Observable} from "rxjs";
import {
    RouteDetails,
    RouteId,
    RouteService
} from "../../../generated/public-transport-api";
import {LoginService} from "../../../auth/login.service";

export const tripsResolver: ResolveFn<Observable<RouteDetails>> = (route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<RouteDetails> => {
    const line: string = route.queryParams['line'];
    const name: string = route.queryParams['name'];
    const routeId: RouteId = {line: line, name: name} as RouteId;

    const authService: LoginService = inject(LoginService);
    const routeService: RouteService = inject(RouteService);

    return routeService.getRouteDetails(authService.getInstance(), routeId);
}