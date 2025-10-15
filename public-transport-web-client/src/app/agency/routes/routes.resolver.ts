import {Resolve, ResolveFn} from '@angular/router';
import {Observable} from "rxjs";
import {inject, Injectable} from "@angular/core";
import {RoutesService} from "./routes.service";
import {Routes, RouteService} from "../../generated/public-transport-api";
import {BrigadeService} from "../brigade/brigade.service";
import {LoginService} from "../../auth/login.service";

@Injectable({
  providedIn: 'root'
})
export class RoutesResolver implements Resolve<Observable<Routes>> {

  constructor(private authService: LoginService, private routeService: RouteService ) {
  }

  resolve(): Observable<Routes> {
    return this.routeService.getRoutes(this.authService.getInstance());
  }
}
