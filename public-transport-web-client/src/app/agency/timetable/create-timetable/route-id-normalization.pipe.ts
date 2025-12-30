import {Pipe, PipeTransform} from "@angular/core";

@Pipe({standalone: true, name: 'routeNameNorm'})
export class RouteNameNormPipe implements PipeTransform {

    transform(value: string): string {
        return value.replaceAll('_', ' ');
    }

}