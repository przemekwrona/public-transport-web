import { Component } from '@angular/core';
import {RoutesService} from "./routes/routes.service";
import {Route, Routes} from "../generated/public-transport";
import {Router} from "@angular/router";

@Component({
  selector: 'app-agency',
  templateUrl: './agency.component.html',
  styleUrl: './agency.component.scss'
})
export class AgencyComponent {

  public routes: Routes;

  constructor(private routeService: RoutesService, private _router: Router) {
    this.routeService.getRoutes().subscribe((routes: Routes) => {
      this.routes = routes;
    })
  }

  public openTrip(route: Route): void {
    const state = {name: route.name, line: route.line};
    console.log(state);
    this._router.navigate(['/agency/trips'], {state: state});
    // this._router.navigateByUrl('/agency/trips', {state: state});
  }

}
