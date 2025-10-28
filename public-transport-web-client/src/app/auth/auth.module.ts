import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {SigninComponent} from "./signin/signin.component";
import {FormsModule} from "@angular/forms";
import {RouterLink} from "@angular/router";


@NgModule({
    declarations: [
        SigninComponent
    ],
    imports: [
        CommonModule,
        FormsModule,
        RouterLink
    ]
})
export class AuthModule {
}
