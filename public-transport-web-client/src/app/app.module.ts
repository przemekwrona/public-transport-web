import {InjectionToken, NgModule} from '@angular/core';
import {AppComponent} from "./app.component";
import {LandingModule} from "./landing/landing.module";
import {BrowserModule} from "@angular/platform-browser";
import {MapModule} from "./transport/map/map.module";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {TransportModule} from "./transport/transport.module";
import {HTTP_INTERCEPTORS, HttpClientModule} from "@angular/common/http";
import {FormsModule} from "@angular/forms";
import {RouterModule} from "@angular/router";
import {routes} from "./app.routes";
import {AgencyModule} from "./agency/agency.module";
import {AuthModule} from "./auth/auth.module";
import {AddHeaderInterceptor} from "./auth/auth.interceptor";
import {BASE_PATH} from "./generated/public-transport";
import { TranslocoRootModule } from './transloco-root.module';
import {BASE_PATH_PDF_API} from "./generated/public-transport-pdf";
import {UserListComponent} from "./agency/user/user-list/user-list.component";
import {CreateUserComponent} from "./agency/user/create-user/create-user.component";

@NgModule({
    imports: [
        BrowserModule,
        RouterModule.forRoot(routes, {useHash: true}),
        HttpClientModule,
        BrowserAnimationsModule,
        TransportModule,
        AgencyModule,
        MapModule,
        LandingModule,
        AuthModule,
        FormsModule,
        TranslocoRootModule,
        UserListComponent,
        CreateUserComponent
    ],
    declarations: [
        AppComponent
    ],
    providers: [
        {provide: HTTP_INTERCEPTORS, useClass: AddHeaderInterceptor, multi: true},
        {provide: BASE_PATH, useValue: 'http://localhost:4200/api/v1'},
        {provide: BASE_PATH_PDF_API, useValue: 'http://localhost:4200/api/v1'}
    ],
    bootstrap: [
        AppComponent
    ]

})
export class AppModule {
}
