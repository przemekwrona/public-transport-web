import {ResolveFn} from '@angular/router';
import {AppUsersResponse, UsersService} from "../../../generated/public-transport";
import {inject} from "@angular/core";

export const usersResolver: ResolveFn<AppUsersResponse> = (route, state) => {
    const usersService: UsersService = inject(UsersService);

    return usersService.getAppUsers();
};
