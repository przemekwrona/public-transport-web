import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TripsComponent} from "./trips.component";

@NgModule({
    declarations: [
        TripsComponent
    ],
    exports: [
      TripsComponent
    ],
    imports: [
        CommonModule
    ]
})
export class TripsModule {
}
