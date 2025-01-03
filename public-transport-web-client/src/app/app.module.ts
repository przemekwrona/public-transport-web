import {NgModule} from '@angular/core';
import {AppComponent} from "./app.component";
import {LandingModule} from "./landing/landing.module";
import {BrowserModule} from "@angular/platform-browser";
import {MapModule} from "./transport/map/map.module";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {TransportModule} from "./transport/transport.module";
import {HttpClientModule} from "@angular/common/http";
import {FormsModule} from "@angular/forms";
import {RouterModule} from "@angular/router";
import {routes} from "./app.routes";
import {AgencyModule} from "./agency/agency.module";
import {AuthModule} from "./auth/auth.module";


@NgModule({
    imports: [
        RouterModule.forRoot(routes),
        BrowserModule,
        HttpClientModule,
        BrowserAnimationsModule,
        TransportModule,
        AgencyModule,
        MapModule,
        LandingModule,
        AuthModule,
        FormsModule
    ],
    exports: [
        RouterModule
    ],
    declarations: [
        AppComponent
    ],
    bootstrap: [
        AppComponent
    ]

})
export class AppModule {
}
