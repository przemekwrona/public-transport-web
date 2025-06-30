import {Resolve, ResolveFn} from '@angular/router';
import {Observable} from "rxjs";
import {inject, Injectable} from "@angular/core";
import {RoutesService} from "./routes.service";
import {Routes, RouteService} from "../../generated/public-transport";
import {BrigadeService} from "../brigade/brigade.service";
import {AuthService} from "../../auth/auth.service";

@Injectable({
  providedIn: 'root'
})
export class RoutesResolver implements Resolve<Observable<Routes>> {

  constructor(private authService: AuthService, private routeService: RouteService ) {
  }

  resolve(): Observable<Routes> {
    return this.routeService.getRoutes(this.authService.getInstance());
  }
}
