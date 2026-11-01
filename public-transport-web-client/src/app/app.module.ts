import {APP_ID, CUSTOM_ELEMENTS_SCHEMA, Injectable, NgModule} from '@angular/core';
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
import {DateAdapter, MAT_DATE_LOCALE, NativeDateAdapter} from "@angular/material/core";
import {NgxSpinnerModule} from "ngx-spinner";
import {providePrimeNG} from "primeng/config";
import Aura from "@primeuix/themes/aura";

@Injectable()
export class MondayDateAdapter extends NativeDateAdapter {
    override getFirstDayOfWeek(): number {
        return 1; // 0 = Sunday, 1 = Monday
    }
}

@NgModule({
    imports: [
        BrowserModule,
        RouterModule.forRoot(routes, {useHash: true, paramsInheritanceStrategy: 'emptyOnly'}),
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
        {provide: HTTP_INTERCEPTORS, useClass: AddHeaderInterceptor, multi: true},
        {provide: API_BASE_PATH, useValue: '/api/v1'},
        {provide: PDF_BASE_PATH, useValue: '/api/v1'},
        provideEnvironmentNgxMask({validation: false}),
        {provide: DateAdapter, useClass: MondayDateAdapter},
        {provide: MAT_DATE_LOCALE, useValue: 'pl-PL'},
        providePrimeNG({
            license: 'eyJpZCI6IjdhNzNlYzg0LTNhMTUtNDM0Mi1iY2M3LWQ5NWUzODRjMWFmMyIsInByb2R1Y3QiOiJwcmltZXVpIiwidGllciI6ImNvbW11bml0eSIsInR5cGUiOiJkZXYiLCJpYXQiOjE3OTM1NzE5MTcsImV4cCI6MTgyNTEwNzkxN30.u4u5cUV5ZSwzeywhC-243HED4KBpbBgmNGaokIkXvwxHiu1FzZplbAFyMboWZ0IP3CwXRXopoZbsm5zMUJENAg',
            theme: {
                preset: Aura,
                options: {
                    darkModeSelector: false
                }
            }
        })
    ],
    bootstrap: [
        AppComponent
    ]

})
export class AppModule {
}
