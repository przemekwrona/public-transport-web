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
    const authService: LoginService = inject(LoginService);
    const routeService: RouteService = inject(RouteService);

    const routeCode: string = route.paramMap.get('routeCode')!;

    return routeService.getRouteDetails(authService.getInstance(), routeCode);
}