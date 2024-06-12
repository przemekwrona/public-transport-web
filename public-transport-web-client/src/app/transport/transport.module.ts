import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TransportComponent} from './transport.component';
import {MapModule} from "./map/map.module";


@NgModule({
  declarations: [TransportComponent],
  imports: [
    CommonModule,
    MapModule
  ],
  exports: [
    TransportComponent
  ]
})
export class TransportModule {
}
