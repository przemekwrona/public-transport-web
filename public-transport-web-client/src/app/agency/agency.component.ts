import {AfterViewInit, Component, HostBinding, OnInit} from '@angular/core';
import {Routes} from "../generated/public-transport-api";
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

  constructor() {
  }

}
