import {NgModule} from '@angular/core';
import {ProfileListComponent} from "./profile-list/profile-list.component";
import {ProfileDetailsComponent} from "./profile-details/profile-details.component";
import {CreateProfileComponent} from "./create-profile/create-profile.component";


@NgModule({
    imports: [
        ProfileDetailsComponent,
        ProfileListComponent,
        CreateProfileComponent
    ],
    exports: [
        ProfileDetailsComponent,
        ProfileListComponent,
        CreateProfileComponent
    ]
})
export class ProfileModule {
}
