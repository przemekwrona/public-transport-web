import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RoutesComponent} from "./routes.component";
import {MatExpansionModule} from "@angular/material/expansion";
import {FieldComponent} from "./field/field.component";
import {MatIconModule} from "@angular/material/icon";
import {CreateRouteComponent} from "./create-route/create-route.component";
import {RouterModule} from "@angular/router";
import {FormsModule} from "@angular/forms";
import {SharedModule} from "../shared/shared.module";

@NgModule({
    declarations: [
        CreateRouteComponent,
        RoutesComponent,
        FieldComponent
    ],
    exports: [
        CreateRouteComponent,
        RoutesComponent
    ],
    imports: [
        CommonModule,
        FormsModule,
        RouterModule,
        SharedModule,
        MatExpansionModule,
        MatIconModule
    ]
})
export class RoutesModule {
}
