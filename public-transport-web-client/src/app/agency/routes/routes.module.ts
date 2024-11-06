import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {RoutesComponent} from "./routes.component";



@NgModule({
  declarations: [
    RoutesComponent
  ],
  exports: [
    RoutesComponent
  ],
  imports: [
    CommonModule
  ]
})
export class RoutesModule { }
