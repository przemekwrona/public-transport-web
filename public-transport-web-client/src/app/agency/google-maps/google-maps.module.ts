import {NgModule} from '@angular/core';
import {GoogleMapsComponent} from "./google-maps.component";


@NgModule({
    imports: [
        GoogleMapsComponent
    ],
    exports: [
        GoogleMapsComponent
    ]
})
export class GoogleMapsModule {
}
