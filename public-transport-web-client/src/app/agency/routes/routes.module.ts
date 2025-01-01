import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RoutesComponent} from "./routes.component";
import {MatExpansionModule} from "@angular/material/expansion";
import {FieldComponent} from "./field/field.component";
import {MatIconModule} from "@angular/material/icon";

@NgModule({
    declarations: [
        RoutesComponent,
        FieldComponent
    ],
    exports: [
        RoutesComponent
    ],
    imports: [
        CommonModule,
        MatExpansionModule,
        MatIconModule
    ]
})
export class RoutesModule {
}
