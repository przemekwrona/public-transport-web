import {RouteStopGraph} from "../../../../generated/public-transport-api";
import {buildStopSequence} from "./stop-sequence";

describe('buildStopSequence', () => {
    it('returns an empty list when graph is missing', () => {
        expect(buildStopSequence(undefined)).toEqual([]);
        expect(buildStopSequence(null)).toEqual([]);
    });

    it('lists spine stops in order', () => {
        const graph: RouteStopGraph = {
            spineStopIds: [1, 2, 3],
            nodes: [
                {stopId: 1, name: 'A'},
                {stopId: 2, name: 'B'},
                {stopId: 3, name: 'C'}
            ],
            branches: []
        };

        expect(buildStopSequence(graph)).toEqual([
            {stopId: 1, name: 'A', variant: false, designation: undefined},
            {stopId: 2, name: 'B', variant: false, designation: undefined},
            {stopId: 3, name: 'C', variant: false, designation: undefined}
        ]);
    });

    it('inserts detour stops after the diverge stop and marks them as variant', () => {
        const graph: RouteStopGraph = {
            spineStopIds: [1, 2, 3, 4],
            nodes: [
                {stopId: 1, name: 'A'},
                {stopId: 2, name: 'B'},
                {stopId: 3, name: 'C'},
                {stopId: 4, name: 'D'},
                {stopId: 10, name: 'Szkoła'}
            ],
            branches: [{
                divergeFromStopId: 2,
                rejoinAtStopId: 4,
                stopIds: [10],
                variantDesignation: 'S'
            }]
        };

        expect(buildStopSequence(graph).map(item => [item.stopId, item.variant, item.designation])).toEqual([
            [1, false, undefined],
            [2, false, undefined],
            [10, true, 'S'],
            [3, false, undefined],
            [4, false, undefined]
        ]);
    });

    it('inserts different-origin stops before the rejoin stop', () => {
        const graph: RouteStopGraph = {
            spineStopIds: [1, 2, 3],
            nodes: [
                {stopId: 1, name: 'A'},
                {stopId: 2, name: 'B'},
                {stopId: 3, name: 'C'},
                {stopId: 20, name: 'Zajezdnia'}
            ],
            branches: [{
                rejoinAtStopId: 2,
                stopIds: [20],
                variantDesignation: 'Z'
            }]
        };

        expect(buildStopSequence(graph).map(item => item.stopId)).toEqual([1, 20, 2, 3]);
        expect(buildStopSequence(graph)[1].variant).toBeTrue();
    });
});
