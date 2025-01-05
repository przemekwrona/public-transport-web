import { Component } from '@angular/core';
import {RoutesService} from "./routes/routes.service";
import {Routes} from "../generated/public-transport";

@Component({
  selector: 'app-agency',
  templateUrl: './agency.component.html',
  styleUrl: './agency.component.scss'
})
export class AgencyComponent {

  public routes: Routes;

  constructor(private routeService: RoutesService) {
    this.routeService.getRoutes().subscribe((routes: Routes) => {
      this.routes = routes;
    })
  }

}
