import {inject} from "@angular/core";
import {ActivatedRoute, ActivatedRouteSnapshot, Data, ResolveFn, RouterStateSnapshot} from "@angular/router";
import {map, Observable} from "rxjs";
import {
    RouteDetails,
    RouteService
} from "../../../generated/public-transport-api";
import {LoginService} from "../../../auth/login.service";

export const tripsResolver: ResolveFn<Observable<RouteDetails>> = (route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<RouteDetails> => {
    const authService: LoginService = inject(LoginService);
    const routeService: RouteService = inject(RouteService);

    const routeCode: string = getRouteParam(route, 'routeCode')!;

    return routeService.getRouteDetails(authService.getInstance(), routeCode);
}

export function routeDetailsData(route: ActivatedRoute): Observable<RouteDetails> | undefined {
    const source = route.pathFromRoot.find(parent => parent.snapshot.data['routeDetails'] !== undefined);
    return source?.data.pipe(map((data: Data) => data['routeDetails'] as RouteDetails));
}

export function getRouteParam(route: ActivatedRouteSnapshot, key: string): string | null {
    let current: ActivatedRouteSnapshot | null = route;
    while (current) {
        const value = current.paramMap.get(key);
        if (value != null) {
            return value;
        }
        current = current.parent;
    }
    return null;
}