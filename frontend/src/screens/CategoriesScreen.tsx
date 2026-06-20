import React, {useCallback, useEffect, useMemo, useState} from 'react';
import {
  FlatList,
  RefreshControl,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import {NativeStackScreenProps} from '@react-navigation/native-stack';
import {parseApiError} from '../api/client';
import * as productApi from '../api/productApi';
import {CategoryCard} from '../components/CategoryCard';
import {ErrorView} from '../components/ErrorView';
import {Header} from '../components/Header';
import {LoadingSpinner} from '../components/LoadingSpinner';
import {CategoriesStackParamList} from '../navigation/types';
import {Category, Product} from '../types';
import {colors} from '../theme/colors';
import {spacing} from '../theme/spacing';

type Props = NativeStackScreenProps<CategoriesStackParamList, 'Categories'>;

export function CategoriesScreen({navigation}: Props) {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const categories = useMemo(
    () => productApi.extractCategories(products),
    [products],
  );

  const loadData = useCallback(async () => {
    setError(null);
    try {
      const data = await productApi.getAllProducts();
      setProducts(data);
    } catch (err) {
      setError(parseApiError(err).message);
    }
  }, []);

  useEffect(() => {
    async function init() {
      setLoading(true);
      await loadData();
      setLoading(false);
    }
    init();
  }, [loadData]);

  const onRefresh = async () => {
    setRefreshing(true);
    await loadData();
    setRefreshing(false);
  };

  if (loading) {
    return <LoadingSpinner fullScreen />;
  }

  if (error) {
    return <ErrorView message={error} onRetry={loadData} />;
  }

  return (
    <View style={styles.container}>
      <Header title="Categories" subtitle="Browse by category" />
      <FlatList
        data={categories}
        keyExtractor={item => String(item.id)}
        numColumns={2}
        contentContainerStyle={styles.list}
        refreshControl={
          <RefreshControl
            refreshing={refreshing}
            onRefresh={onRefresh}
            colors={[colors.primary]}
          />
        }
        ListEmptyComponent={
          <Text style={styles.empty}>No categories available</Text>
        }
        renderItem={({item}: {item: Category}) => (
          <CategoryCard
            category={item}
            onPress={() =>
              navigation.navigate('CategoryProducts', {
                categoryId: item.id,
                categoryName: item.name,
              })
            }
          />
        )}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  list: {
    padding: spacing.sm,
    paddingBottom: spacing.xxl,
  },
  empty: {
    textAlign: 'center',
    color: colors.textSecondary,
    marginTop: spacing.xxxl,
  },
});
