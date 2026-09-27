import {APP_ID, APP_INITIALIZER, CUSTOM_ELEMENTS_SCHEMA, NgModule} from '@angular/core';
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
import {KeycloakAuthService} from "./auth/keycloak-auth.service";
import {LoginService} from "./auth/login.service";
import {BASE_PATH as API_BASE_PATH} from "./generated/public-transport-api";
import {TranslocoRootModule} from './transloco-root.module';
import {BASE_PATH as PDF_BASE_PATH} from "./generated/public-transport-pdf-api";
import {UserListComponent} from "./agency/user/user-list/user-list.component";
import {CreateUserComponent} from "./agency/user/create-user/create-user.component";
import {provideEnvironmentNgxMask} from "ngx-mask";
import {DateAdapter, MAT_DATE_LOCALE, NativeDateAdapter} from "@angular/material/core";
import {NgxSpinnerModule} from "ngx-spinner";

export function initKeycloak(keycloakAuth: KeycloakAuthService, loginService: LoginService): () => Promise<void> {
    return () => keycloakAuth.init().then(() => loginService.captureKeycloakSession());
}

export class MondayDateAdapter extends NativeDateAdapter {
    override getFirstDayOfWeek(): number {
        return 1; // 0 = Sunday, 1 = Monday
    }
}

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
        CreateUserComponent,
        NgxSpinnerModule.forRoot({ type: 'ball-scale-multiple' })
    ],
    declarations: [
        AppComponent
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
    providers: [
        { provide: APP_ID, useValue: 'nastepna-stacja' },
        {
            provide: APP_INITIALIZER,
            useFactory: initKeycloak,
            deps: [KeycloakAuthService, LoginService],
            multi: true
        },
        {provide: HTTP_INTERCEPTORS, useClass: AddHeaderInterceptor, multi: true},
        {provide: API_BASE_PATH, useValue: '/api/v1'},
        {provide: PDF_BASE_PATH, useValue: '/api/v1'},
        provideEnvironmentNgxMask({validation: false}),
        {provide: DateAdapter, useClass: MondayDateAdapter},
        {provide: MAT_DATE_LOCALE, useValue: 'pl-PL'}
    ],
    bootstrap: [
        AppComponent
    ]

})
export class AppModule {
}
