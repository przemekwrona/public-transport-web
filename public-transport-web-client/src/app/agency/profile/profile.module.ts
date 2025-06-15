import {NgModule} from '@angular/core';
import {ProfileComponent} from "./profile.component";
import {ProfileListComponent} from "./profile-list/profile-list.component";


@NgModule({
    imports: [
        ProfileComponent,
        ProfileListComponent
    ],
    exports: [
        ProfileComponent,
        ProfileListComponent
    ]
})
export class ProfileModule {
}
