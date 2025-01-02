import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {ProfileComponent} from "./profile.component";
import {FormsModule} from "@angular/forms";



@NgModule({
  declarations: [
      ProfileComponent
  ],
  exports: [
    ProfileComponent
  ],
  imports: [
    CommonModule,
    FormsModule
  ]
})
export class ProfileModule { }
