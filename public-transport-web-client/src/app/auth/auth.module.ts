import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {SigninComponent} from "./signin/signin.component";
import {FormsModule} from "@angular/forms";


@NgModule({
    declarations: [
        SigninComponent
    ],
    imports: [
        CommonModule,
        FormsModule
    ]
})
export class AuthModule {
}
