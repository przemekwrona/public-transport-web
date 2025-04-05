import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {GoogleMapsComponent} from "./google-maps.component";
import {GtfsService} from "../../generated/public-transport";


@NgModule({
  declarations: [
      GoogleMapsComponent
  ],
  imports: [
    CommonModule
  ],
  providers: [
    GtfsService
  ]
})
export class GoogleMapsModule { }
