"use client";

import { AntdInferencer } from "@refinedev/inferencer/antd";

export const TripsList = () => {
    return <AntdInferencer resource="trips" action="list" />;
};
