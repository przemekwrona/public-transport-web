import {APP_ID, InjectionToken, NgModule} from '@angular/core';
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
import {BASE_PATH as API_BASE_PATH} from "./generated/public-transport-api";
import {TranslocoRootModule} from './transloco-root.module';
import {BASE_PATH as PDF_BASE_PATH} from "./generated/public-transport-pdf-api";
import {UserListComponent} from "./agency/user/user-list/user-list.component";
import {CreateUserComponent} from "./agency/user/create-user/create-user.component";
import {provideEnvironmentNgxMask} from "ngx-mask";

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
        { provide: APP_ID, useValue: 'nastepna-stacja' },
        {provide: HTTP_INTERCEPTORS, useClass: AddHeaderInterceptor, multi: true},
        {provide: API_BASE_PATH, useValue: '/api/v1'},
        {provide: PDF_BASE_PATH, useValue: '/api/v1'},
        provideEnvironmentNgxMask({validation: false})
    ],
    bootstrap: [
        AppComponent
    ]

})
export class AppModule {
}
