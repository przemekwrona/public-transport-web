import {Resolve, ResolveFn} from '@angular/router';
import {Observable} from "rxjs";
import {Injectable} from "@angular/core";
import {RoutesService} from "./routes.service";
import {Routes} from "../../generated/public-transport";

@Injectable({
  providedIn: 'root'
})
export class RoutesResolver implements Resolve<Observable<Routes>> {

  constructor(private routesService: RoutesService) {
  }

  resolve(): Observable<Routes> {
    return this.routesService.getRoutes();
  }
}
