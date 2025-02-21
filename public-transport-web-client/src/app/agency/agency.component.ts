import {AfterViewInit, Component, HostBinding, OnInit} from '@angular/core';
import {RoutesService} from "./routes/routes.service";
import {Route, Routes} from "../generated/public-transport";
import {Router} from "@angular/router";
import KTComponents from "../../metronic/core";
import KTLayout from '../../metronic/app/layouts/demo1';

@Component({
  selector: 'app-agency',
  templateUrl: './agency.component.html',
  styleUrl: './agency.component.scss'
})
export class AgencyComponent implements OnInit, AfterViewInit {

  @HostBinding('class') hostClass = 'flex grow h-full';

  public routes: Routes;

  ngAfterViewInit(): void {
    KTComponents.init();
    KTLayout.init();
  }

  ngOnInit(): void {
  }

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
