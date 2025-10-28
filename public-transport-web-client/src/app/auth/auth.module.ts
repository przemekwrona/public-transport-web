import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {SigninComponent} from "./signin/signin.component";
import {FormsModule} from "@angular/forms";
import {RouterLink} from "@angular/router";
import {TranslocoPipe} from "@jsverse/transloco";


@NgModule({
    declarations: [
        SigninComponent
    ],
    imports: [
        CommonModule,
        FormsModule,
        RouterLink,
        TranslocoPipe
    ]
})
export class AuthModule {
}
