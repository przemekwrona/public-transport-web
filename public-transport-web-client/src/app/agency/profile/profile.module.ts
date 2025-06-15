import {NgModule} from '@angular/core';
import {ProfileListComponent} from "./profile-list/profile-list.component";
import {ProfileDetailsComponent} from "./profile-details/profile-details.component";


@NgModule({
    imports: [
        ProfileDetailsComponent,
        ProfileListComponent
    ],
    exports: [
        ProfileDetailsComponent,
        ProfileListComponent
    ]
})
export class ProfileModule {
}
