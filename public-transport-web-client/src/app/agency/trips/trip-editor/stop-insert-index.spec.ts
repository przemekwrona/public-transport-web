import {StopCoordinate, stopInsertIndex} from './stop-insert-index';

describe('stopInsertIndex', () => {
    const line = (longitudes: number[]): StopCoordinate[] =>
        longitudes.map(lon => ({lat: 50, lon}));

    it('appends when the trip has fewer than two stops', () => {
        expect(stopInsertIndex([], {lat: 50, lon: 1})).toBe(0);
        expect(stopInsertIndex(line([0]), {lat: 50, lon: 1})).toBe(1);
    });

    it('appends when the new stop has no coordinates', () => {
        expect(stopInsertIndex(line([0, 1, 2, 3, 4, 5]), {lat: null, lon: null})).toBe(6);
    });

    it('inserts between the two nearest interior stops', () => {
        const existing = line([0, 1, 2, 3, 4, 5, 6]);

        expect(stopInsertIndex(existing, {lat: 50, lon: 2.4})).toBe(3);
    });

    it('inserts between non-adjacent interior stops, after the earlier one', () => {
        const existing: StopCoordinate[] = [
            {lat: 50, lon: 0},
            {lat: 50, lon: 1},
            {lat: 50, lon: 2},
            {lat: 60, lon: 10},
            {lat: 50, lon: 4},
            {lat: 50, lon: 5},
            {lat: 50, lon: 6}
        ];

        expect(stopInsertIndex(existing, {lat: 50, lon: 3})).toBe(3);
    });

    it('appends when the two nearest are stops 1 and 2', () => {
        expect(stopInsertIndex(line([0, 1, 2, 3, 4, 5, 6]), {lat: 50, lon: 0.2})).toBe(7);
    });

    it('appends when one of the two nearest is stop 2', () => {
        expect(stopInsertIndex(line([0, 1, 2, 3, 4, 5, 6]), {lat: 50, lon: 1.6})).toBe(7);
    });

    it('appends when the two nearest are the penultimate and the last stop', () => {
        expect(stopInsertIndex(line([0, 1, 2, 3, 4, 5, 6]), {lat: 50, lon: 5.8})).toBe(7);
    });

    it('appends when one of the two nearest is the penultimate stop', () => {
        expect(stopInsertIndex(line([0, 1, 2, 3, 4, 5]), {lat: 50, lon: 3.6})).toBe(6);
    });

    it('appends on a short trip where every stop is an endpoint', () => {
        expect(stopInsertIndex(line([0, 1, 2, 3]), {lat: 50, lon: 1.5})).toBe(4);
    });

    it('ignores stops without coordinates when choosing the nearest pair', () => {
        const existing: StopCoordinate[] = [
            {lat: 50, lon: 0},
            {lat: 50, lon: 1},
            {lat: null, lon: null},
            {lat: 50, lon: 3},
            {lat: 50, lon: 4},
            {lat: 50, lon: 5},
            {lat: 50, lon: 6}
        ];

        expect(stopInsertIndex(existing, {lat: 50, lon: 3.2})).toBe(4);
    });
});
