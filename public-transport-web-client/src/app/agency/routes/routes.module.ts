import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatExpansionModule} from "@angular/material/expansion";
import {FieldComponent} from "./field/field.component";
import {MatIconModule} from "@angular/material/icon";
import {CreateRouteComponent} from "./create-route/create-route.component";
import {RouterModule} from "@angular/router";
import {FormsModule} from "@angular/forms";
import {SharedModule} from "../shared/shared.module";
import {MatDialogModule} from "@angular/material/dialog";
import {RouteListComponent} from "./route-list/route-list.component";
import {PdfService} from "../../generated/public-transport-pdf-api";

@NgModule({
    declarations: [
        CreateRouteComponent,
        FieldComponent
    ],
    exports: [
        CreateRouteComponent,
        RouteListComponent
    ],
    imports: [
        CommonModule,
        FormsModule,
        RouterModule,
        SharedModule,
        MatExpansionModule,
        MatIconModule,
        MatDialogModule,
        RouteListComponent
    ], providers: [
        PdfService
    ]
})
export class RoutesModule {
}
