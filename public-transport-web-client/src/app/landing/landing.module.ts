import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {LandingComponent} from "./landing.component";
import {FontAwesomeModule} from "@fortawesome/angular-fontawesome";



@NgModule({
  imports: [
    CommonModule,
    FontAwesomeModule
  ],
  declarations: [
    LandingComponent
  ],
  exports: [
    LandingComponent
  ]
})
export class LandingModule { }
